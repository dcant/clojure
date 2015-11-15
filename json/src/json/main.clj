(ns json.main
	(:require [cheshire.core :refer :all]))

(defn jsonalike2seq
	"load origin json like file content into map seq"
	[file]
	(map #(parse-string % true) (clojure.string/split (slurp file) #"#\n")))

(defn seq2json
	"convert map seq from func jsonalike2seq to json string"
	[x]
	(generate-string {:abstract x}))

(defn json2map
	"load json data into map"
	[json]
	(parse-string json true))

(defn mergeheads
	[m1 m2]
	(reduce
		(fn [ans [k v]]
			(assoc m1 k v))
		m1
		m2))

(defn mergerows
	[r1 r2]
	(reduce
		(fn [ans item]
			(conj ans item))
		r1
		r2))

(defn mergejson
	[ans cont]
	(reduce
		(fn [res [k v]]
			(if (nil? (k res))
				(assoc res k v)
				(assoc res
					k (assoc (assoc {} :head (mergeheads (:head (k res)) (:head v)))
						:rows (mergerows (:rows (k res)) (:rows v))))))
		ans
		cont))

(defn trans
	[k coll pid]
	(reduce
		(fn [ans item]
			(let [cont (reduce (fn [js [c v]]
									(if (coll? v)
										(mergejson js (trans c v (:id item)))
										(assoc js
											k (assoc (assoc {}
														:head (assoc (:head (k js)) c 0))
												:rows (conj [] (assoc (first (:rows (k js))) c v))))))
								(assoc {} k (assoc (assoc {} :head {}) :rows []))
								item)]
				(mergejson ans (if (nil? pid)
									cont
									(assoc {}
											k (assoc (assoc {} :head (:head (k cont)))
													:rows (conj [] (assoc (first (:rows (k cont))) :parentid pid))))))))
		{}
		coll))

(defn transfer
	[m]
	(trans :attractions (:attractions m) nil))

(defn -main
	([]
		(let [x (transfer (json2map (slurp "/home/zang/code/clojure/json/attractions-stage2-09")))]
			(spit "/tmp/res.json" (generate-string x {:pretty true}))))
	([infile]
		(let [x (transfer (json2map (seq2json (jsonalike2seq "/tmp/foo"))))]
			(spit "/tmp/bar" (generate-string x {:pretty true}))))
	([infile outfile]
		(let [x (transfer (json2map (seq2json (jsonalike2seq infile))))]
			(spit outfile (generate-string x {:pretty true})))))